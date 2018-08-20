import React, { Component } from 'react'
import styled from 'styled-components'
import Avatar from '../components/Avatar'
import profile from '../profile.jpg'
import ImageTitleValue, {ImageContent} from '../components/ImageTitleValue';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import PointLabel from '../components/PointLabel';

const Wrapper = styled.div`
  width: 100%;
  height: 70px;
  display: flex;
  align-items: center;
`

const Logo = styled.div`
  min-width: 120px;
  background-color: white;
  text-align: center;
  line-height: 70px;
  font-size: 30px;
  color: hsl(203, 49%, 26%);
`

const MyWrapper = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: hsl(203, 49%, 26%);
  padding: 0 0 0 20px;
`

const UserStats = styled.div`
  width: 100%;
  display: flex;
  justify-content: space-evenly;
  align-items: center;
`

const AvatarPane = styled.div`
  min-width: 60px;
  background-color: hsl(203, 70%, 10%);
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
`

export default class Header extends Component {
  render() {
    return (
      <Wrapper>
        <Logo>
          OASIS
        </Logo>
        <MyWrapper>
          <UserStats>
            <ImageContent color="#fff" imgColor="gold" imgSize={24} image={<FontAwesomeIcon icon="coins" />}>
              <PointLabel color="gold" value={(2345).toLocaleString()} delta={432} annotation="THIS WEEK" />
            </ImageContent>
            <ImageContent color="#fff" imgColor="#ff7f82" imgSize={24} image={<FontAwesomeIcon icon="award" />}>
              <PointLabel color="#ff7f82" value={14} />
            </ImageContent>
            <ImageContent color="#fff" imgColor="#72e5b7" imgSize={24} image={<FontAwesomeIcon icon="map-marker-alt" />}>
              <PointLabel color="#72e5b7" value={'73%'} />
            </ImageContent>
            <ImageContent color="#fff" imgColor="#ec8c54" imgSize={24} image={<FontAwesomeIcon icon="trophy" />}>
              <PointLabel color="#ec8c54" value={8} />
            </ImageContent>
          </UserStats>
          <AvatarPane>
            <Avatar size={32} image={profile} />
          </AvatarPane>
        </MyWrapper>
      </Wrapper>
    )
  }
}
